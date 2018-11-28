# Order Fulfillment Routes in Fuse 7.x

This project is centered around an order fulfillment system for a hardware retailer.  Orders are consumed by Fuse from either a File or JMS-based interface, validated then processed according to their category.  Each order request is then individually transformed into an order fulfillment message, then transferred to the corresponding system for processing.

Part of the project is implemented in Fuse by a Developer.  The remaining implementation should be completed by a *Citizen Integrator* in Fuse Online.  That way, both Developer's and Citizen Integrator's can collaborate to create a holistic integration solution.

The Camel routes used in this example are explained by the following diagram:

![CBR Diagram](src/img/order_fullfilment.png)

## Prerequisite installation steps

### Setup Fuse Online

1.  Create a new Fuse Online OpenShift project using `oc new-project fuse-online`
2.  Download the Fuse Online install script by executing `wget https://raw.githubusercontent.com/syndesisio/fuse-online-install/1.4/install_ocp.sh`
3.  Create the CRD using the following command: `oc get crd`.
4.  Run the CRD install script using the following command: `bash install_ocp.sh --setup`
5.  Install Fuse Online using this command: `bash install_ocp.sh`

### Setup OpenShift Project

1. Via the CLI, create a new project for the deployment: `oc new-project order-fulfillment`

### Setup Postgres Database

1. Open a browser window and navigate to your OpenShift web console.

1. Click on the `order-fulfillment` project.

1. Click on **Browse Catalog**, then navigate to the **Databases** menu and select **Postgres**.  From there, select the **PostgreSQL** (Ephemeral) template.

    ![00-select-postgres.png](src/img/00-select-postgres.png "Select Postgres")

1. In the pop-up window that appears, click the **Next** button to reach the **Configuration** page.  Update **PostgreSQL Connection Username** to `dbuser` and **PostgreSQL Connection Password** to `password`.

    ![00-postgres-credentials.png](src/img/00-postgres-credentials.png "Postgres Credentials")

1. Click **Next** and ensure *Do not Bind at this time* is selected.  Click **Create** to generate the service.

1. Navigate to **Services**, locate the *postresql* Service, then create a route.

### Setup AMQ 6.3 Broker

1.  Using the same `order-fulfillment` project, execute the following via the CLI:

	```bash
	echo '{"kind": "ServiceAccount", "apiVersion": "v1", "metadata": {"name": "amqsa"}}' | oc create -f -
	```
1. Add the view role to the SA:

	```bash
	oc policy add-role-to-user view system:serviceaccount:order-fulfillment:amqsa
	```
1. Execute the following command from the local order-fulfillment directory:

	```bash
	oc secrets new amq-app-secret src/main/resources/broker.ks src/main/resources/broker.ts
	```

1. Navigate back to the OCP web console and Click "Add to Project".  Select the *Middleware > Integration* tab, then select the **JBoss AMQ 6.3 (Ephemeral with SSL)** icon.

1. On the configuration page, enter `openwire,amqp` as the **A-MQ Protocols**.  Enter `admin` for the **A-MQ Username** and **A-MQ Password**.  Enter `topsecret` for the **Trust Store Password** and **A-MQ Keystore Password**.  Click through the rest of the pages, but ensure **Do not bind at this time** is selected.

1. Once the broker has started, navigate to Services and create a route for the `broker-amq-tcp-ssl` service.  Ensure to secure the route with 'Passthrough' SSL termination.

## Build & Run

### Running locally

1.  Update the `src/main/resources/application.properties` with the new AMQ route hostname

	```bash
	# AMQ broker properties
	activemq.broker.url=failover://ssl://broker-amq-tcp-ssl-order-fulfillment.apps.lowes-7031.openshiftworkshop.com:443
	```
	
1.  Via the CLI, type `oc get pods` to display the running pods.  Copy the pod name for the `postgresql` service.

1. Enable port forwarding of the postgres service by typing the following via the CLI (but replacing the pod name): `oc port-forward <postgres pod> 5432`.

1. Via the command-line, you should be able to run this project locally using mvn, and it should work as expected:

	```bash
	mvn spring-boot:run
	```

## Running on JBoss Fuse (Karaf Standalone)
You will need to install this example first:
  
> mvn install

Install Derby database and JDBC connection pool in Fuse with:

> osgi:install -s mvn:commons-pool/commons-pool/1.6

> osgi:install -s mvn:commons-dbcp/commons-dbcp/1.4

> osgi:install -s mvn:org.apache.derby/derby/10.10.1.1

Install into Fuse with:

> features:addurl mvn:org.redhat.examples/transformation-and-cbr/1.0.0-SNAPSHOT/xml/features

> features:install transformation-and-cbr

And you can see the application running by tailing the logs

  log:tail

And you can use ctrl + c to stop tailing the log.

## Running on OpenShift (CDK, Minishift or OpenShift Enterprise)
Once you have your OpenShift environment running, login as the admin user using oc tools.

Ensure you have the FIS / AMQ image streams installed in the OpenShift namespace.  If not, install them using the following command:

> oc project openshift
> BASEURL=https://raw.githubusercontent.com/jboss-fuse/application-templates/GA
> oc replace --force -n openshift -f ${BASEURL}/fis-image-streams.json
> oc replace --force -n openshift -f https://raw.githubusercontent.com/jboss-openshift/application-templates/master/jboss-image-streams.json
> BASEURL=https://raw.githubusercontent.com/jboss-openshift/application-templates/master/amq
> oc replace --force -n openshift -f ${BASEURL}/amq62-basic.json

Next, setup the AMQ 62 basic image in your OpenShift project

> oc new project karaf-amq
> oc process amq62-basic -v APPLICATION_NAME=broker,MQ_USERNAME=admin,MQ_PASSWORD=admin -n karaf-amq | oc create -f -
> echo '{"kind": "ServiceAccount", "apiVersion": "v1", "metadata": {"name": "amqsa"}}' | oc create -f -
> oc policy add-role-to-user view system:serviceaccount:karaf-amq:amqsa

Next, edit the deployment config for the AMQ broker to include the Service Account

> oc edit dc/broker-amq

Add the serviceAccount and serviceAccountName parameters to the spec field, and specify the service account you want to use.

> spec:
>      securityContext: {}
>      serviceAccount: serviceaccount
>      serviceAccountName: amqsa

Update the src/main/resources/amq.properties file and uncomment the following line OpenShift activemq.broker.url property.  Besure to comment the Karaf property that you replace.

Install the example to Openshift using the following commands:

> mvn clean install
> mvn fabric8:deploy

## Testing via the Fuse Management Console

From the Fuse console, select the ActiveMQ tab, and inject sample XML messages (found in src/data) into the payload window:

![amq-console](src/img/amqTestMessage.png)

Getting Help
============================

If you hit any problems please let the Fuse team know on the forums
  [https://community.jboss.org/en/jbossfuse]
