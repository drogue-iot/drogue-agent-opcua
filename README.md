# Drogue IoT OPC UA Agent

This is an OPC UA agent for transmitting data to [Drogue IoT cloud](https://github.com/drogue-iot/drogue-cloud).

If you want to learn more about Drogue IoT, please visit its website: https://drogue.io.

## Running, building, …

This is a standard Quarkus application, it can be run in local development by executing:

    mvn compile quarkus:dev

## Using

You will need to configure the following settings, e.g. by adding a file named `application-prod.yaml`:

~~~yaml
drogue:
  agent:
    opcua:
      endpointUrl: opc.tcp://localhost:4840/
      items:
        - ns=2;s=HelloWorld/Dynamic/Double
        - ns=2;s=HelloWorld/DataAccess/AnalogValue
        - ns=2;s=ShouldNotExist
  cloud:
    uplink:
      "client/mp-rest/url": https://http.sandbox.drogue.cloud
      application: my-application
      device: my-device
      password: my-password
~~~

The agent will connect to the OPC UA server and subscribe to th

## Known issues

The following items are known issues, and should get fixed in the not too distant future. Pull-requests welcome ;-)

* [ ] Certificates are currently not properly handled.
* [ ] X.509 client authentication towards OPC UA is not supported.
* [ ] A lot of configuration options (like publish rate) should be configurable, but are not.
