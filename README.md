# Kinota&trade; Server

Kinota&trade; Server is a Spring Boot application that makes it easy to run 
[Fraunhofer IOSB SensorThingsServer](https://github.com/FraunhoferIOSB/SensorThingsServer) in cloud environments.
In addition to the functionality provided by Fraunhofer IOSB SensorThingsServer, Kinota Server includes support
the following security features: TLS and JavaScript Web Token (JWT) authentication.

## License

Kinota Server is licensed under GNU Lesser General Public License v3.0 (LGPLv3); you may not use
this file except in compliance with the License.  For more information, please refer to
the included [license](LICENSE.txt).

## Copyright

Kinota&trade; Server &copy;2017 CGI Group Inc.

## Requirements

Kinota Server requires the following to build and run:
* Java 8
* Maven 3 (for building only)
* PostgreSQL 9.4+ with PostGIS extensions

## Building Kinota Server

```bash
mvn clean install
```

> Note that only Kinota Server-specific unit tests are run; Fraunhofer IOSB SensorThingsServer tests are not
run during the Kinota Server build process.

To run without unit tests

```bash
mvn clean install -DskipTests=true
```

## Initializing SensorThingsServer database

Kinota Server allows for the SensorThingsServer database to be initialized using the `DatabaseStatus` Servlet that is 
included with SensorThingsServer.  However, for security reasons, Kinota Server will only allow `DatabaseStatus` to be 
run by user agents connecting to the server from `localhost` (i.e. 127.0.0.1).  Because this can be cumbersome to do
in server- or cloud-based environments, you can also manually use [Liquibase](http://www.liquibase.org/) to initialize
the database.  The following steps outline roughly how to do this:
* Install Liquibase
* Download the `tables.xml` and `postgresTriggers.sql` from the [Fraunhofer IOSB SensorThingsServer repo](https://github.com/FraunhoferIOSB/SensorThingsServer/tree/5625ef34a614b1201ec31ad5feba0811bc031cc3/SensorThingsServer.SQL/src/main/resources/liquibase)
  * Note that the above link links to version 1.1 of SensorThingsServer, which is what Kinota Server currently builds against]
* Download [PostgreSQL JDBC jar](http://repo.maven.apache.org/maven2/org/postgresql/postgresql/9.4.1212/postgresql-9.4.1212.jar)
* Run `liquibase update`:
    ```bash
    liquibase --driver=org.postgresql.Driver \
    --classpath=/path/to/postgresql-9.4.1212.jar \
    --changeLogFile=tables.xml \
    --url="jdbc:postgresql://MY.DB.SERVER.NAME.OR.ADDRESS:5432/sensorthings?ssl=true" \
    --username="MY_USERNAME" \
    --password="MY_PASSWORD" \
    update
    ```
    
    > Note that depending on how your PostgreSQL server is configured, you may need to omit `?ssl=true` from the --url
    parameter above.  It is recommended that you use SSL if you are connecting to the database via the Internet, rather
    than within a DMZ/private network.
    
## Running Kinota Server

Kinota Server can be easily configured via [Spring Boot](https://projects.spring.io/spring-boot/).
In fact, all SensorThingsServer configuration parameters, which are typically managed by editing [context.xml](https://github.com/FraunhoferIOSB/SensorThingsServer/blob/master/SensorThingsServer/src/main/webapp/META-INF/context.xml),
can be configured for Kinota Server using Spring Boot configuration [conventions](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).  For a listing of possible configuration options, 
see [application.yml](src/main/resources/application.yml).

Kinota Server can be run in three different ways: (1) development mode; (2) production mode (with a web server such as
 NGINX serving as a reverse proxy that handles HTTPS); and (3) standalone mode (with HTTPS handled by the embedded Tomcat server).

### Development mode
Here is an example of how run in Kinota Server in development mode:

```bash
sudo java -jar /path/to/kinota-server.jar --spring.profiles.active=dev --sta.datasource.url="jdbc:postgresql://MY.DB.SERVER.NAME.OR.ADDRESS:5432/sensorthings?ssl=true" --sta.datasource.username="MY_USERNAME" --sta.datasource.password="MY_PASSWORD" --sta.serviceRootUrl="https://MY.SERVER.HOSTNAME.OR.ADDRESS/SensorThingsService"
```

> Note that this will use a self-signed certificate and private key included in the Kinota Server repo and JAR file.  
This should under no circumstances be used to secure an actual server, and is meant for development purposes only!

### Production mode
To use Kinota Server in production mode, first install and configure web server.  An example NGINX configuration might
look like:

```
server {
    listen 80;
    server_name MY.SERVER.HOSTNAME.OR.ADDRESS;
    return 301 https://$host$request_uri;
}
 
server {
    listen 443 ssl;
 
    server_name MY.SERVER.HOSTNAME.OR.ADDRESS;
 
    ssl_certificate /path/to/MY.SERVER.HOSTNAME.OR.ADDRESS/cert.pem;
    ssl_certificate_key /path/to/MY.SERVER.HOSTNAME.OR.ADDRESS/privkey.pem;
 
    # SensorThingsAPI
    location /SensorThingsService {
        proxy_pass http://127.0.0.1:3001;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Proto https;
        proxy_cache_bypass $http_upgrade;
    }
}
```

> You can freely and easily obtain an SSL certificate from [Let's Encrypt](https://letsencrypt.org).

To run Kinota Server in production mode, do something like the following:

```bash
java -jar /path/to/kinota-server.jar --spring.profiles.active=prod --sta.datasource.url="jdbc:postgresql://MY.DB.SERVER.NAME.OR.ADDRESS:5432/sensorthings?ssl=true" --sta.datasource.username="MY_USERNAME" --sta.datasource.password="MY_PASSWORD" --sta.serviceRootUrl="https://MY.SERVER.HOSTNAME.OR.ADDRESS/SensorThingsService"
```

> You will probably want to run this as a service on whatever operating system you are using.  Here is an example for
modern versions of Debian:
```
nano /etc/systemd/system/kinota-server.service
```
Contents:
```
[Unit]
Description=Kinota Server
After=syslog.target
 
[Service]
Type=forking
ExecStart=/bin/sh -c nohup java -jar /path/to/kinota-server.jar --spring.profiles.active=prod --sta.datasource.url="jdbc:postgresql://MY.DB.SERVER.NAME.OR.ADDRESS:5432/sensorthings?ssl=true" --sta.datasource.username="MY_USERNAME" --sta.datasource.password="MY_PASSWORD" --sta.serviceRootUrl="https://MY.SERVER.HOSTNAME.OR.ADDRESS/SensorThingsService" >> /var/log/kinota-server.log 2>&1 &'
```

Enable new service on boot and run it now:
```
systemctl daemon-reload // Reloads changes made to the systemd directory
systemctl enable kinota-server // Enables the service on reboot
systemctl start kinota-server // Starts the service
systemctl status kinota-server // Make sure service is active
```

### Standalone mode

You can run Kinota Server in standalone mode using the built-in Tomcat server running inside of Spring Boot
instead of an external web server.  In this case, you will need to generate an SSL/TLS certificate and import it 
into a Java-compatible keystore.  Here are some rough instructions for doing this using 
[Let's Encrypt](https://letsencrypt.org/) certificates.

Before getting started, install [Certbot](https://certbot.eff.org/).  Then:

```bash
certbot certonly -a standalone -d MY.SERVER.HOSTNAME.OR.ADDRESS
cd /etc/letsencrypt/live/MY.SERVER.HOSTNAME.OR.ADDRESS/
openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out keystore.p12 -name tomcat -CAfile chain.pem -caname root
chmod 400 keystore.p12 
mkdir /etc/kinota
mv keystore.p12 /etc/kinota/
cd /etc/kinota
keytool -importkeystore -deststorepass "changeit" -destkeypass "changeit" -destkeystore keystore.jks -srckeystore keystore.p12 -srcstoretype PKCS12 -srcstorepass "" -alias tomcat
chmod 400 keystore.jks
cd ..
chmod 500 kinota/
```

Then you should be able to run Kinota Server as follows:

```bash
java -jar /path/to/kinota-server.jar --spring.profiles.active=standalone --sta.datasource.url="jdbc:postgresql://MY.DB.SERVER.NAME.OR.ADDRESS:5432/sensorthings?ssl=true" --sta.datasource.username="MY_USERNAME" --sta.datasource.password="MY_PASSWORD" --sta.serviceRootUrl="https://MY.SERVER.HOSTNAME.OR.ADDRESS/SensorThingsService"
```
> You can also configure the location of the keystore by specifying the `--server.ssl.key-store` runtime parameter.

## Configuring JWT authentication

Kinota Server's JWT authentication implementation uses an in-memory database to store client IDs and keys.
The contents of this database is loaded from a file on disk when Kinota Server starts up.  When running in 
development mode, the client IDs and keys are loaded from [data.sql](src/main/resources/data.sql), which is stored
in the Kinota Server repo and JAR file.  When running in production or standalone modes, these data are loaded from
a file pointed to by the Spring variable named `spring.datasource.data`, whose default value in application.yml is 
`/etc/kinota/agents.sql`.  You can change this value to point to any file on your server 
(just pass `--spring.datasource.data=file:/path/to/my/file.sql` as an argument at startup).  The following instructions
show you how to create `/etc/kinota/agents.sql` on your server:

```
mkdir /etc/kinota
nano /etc/kinota/agents.sql
```

Contents:
```
insert into agents(id, key) values ('265a85f2-00fd-4294-b01e-c120ea103d0c', 'a74ce162-0dc2-4b11-bf0a-e3c950685559');
insert into agents(id, key) values ('289db907-7958-4566-b1be-ea8d50ea6200', '05aaef2c-4281-4cb4-b1ec-28ff53780dee');
insert into agents(id, key) values ('78141f2b-4fb4-4837-92e3-a876046ab435', 'df1af5ff-4139-4335-801c-decec39d517e');
```

> ID and keys are just UUIDs, so feel free to generate those however you please.  Note that you should not use the
above IDs and keys on your server as they would be easily guessed by malicious parties!

### Performing JWT authentication

Currently, Kinota Server allows unauthenticated access to all HTTP `GET` requests.  All `POST`, `PUT`, `PATCH`, and 
`DELETE` requests require a valid JWT authentication toekn.  You can obtain a JWT authentication token by making the 
following HTTP request:

Request: `POST https://MY.SERVER.HOSTNAME.OR.ADDRESS/SensorThingsService/auth/login`

Headers:

Header | Value
--- | ---
Content-Type | application/JSON

Body:
```json
{"id":"265a85f2-00fd-4294-b01e-c120ea103d0c","key":"a74ce162-0dc2-4b11-bf0a-e3c950685559"}
```

Response:
```json
{
"token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyNjk0YjllMS1jZTU5LTRmZDUtYjk1ZS1hYTFjNzgwZTgxNTgiLCJyb2xlcyI6IkNHSVNUX0RFVklDRSIsImV4cCI6MTQ5NDQ3MDI2NH0.VBVsKSJ3_EaSFHlP1uUIcXMpytU4UP0jls8iz556Y4ky_6e4tTcYtUiNutKng_G8y0zxTNDk_bvbO_LJmAqrjA"
}
```

Once you have a token from an authentication response, you can make an authenticated request as follows.  In this
example, we will create a new ObservedProperty.

Request: `POST https://MY.SERVER.HOSTNAME.OR.ADDRESS/SensorThingsService/v1.0/ObservedProperties`

Headers:

Header | Value
--- | ---
Content-Type | application/JSON
Authorization | Bearer JWT_TOKEN

Based on the above example, the authorization header would be:
```
Authorization Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyNjk0YjllMS1jZTU5LTRmZDUtYjk1ZS1hYTFjNzgwZTgxNTgiLCJyb2xlcyI6IkNHSVNUX0RFVklDRSIsImV4cCI6MTQ5NDQ3MDI2NH0.VBVsKSJ3_EaSFHlP1uUIcXMpytU4UP0jls8iz556Y4ky_6e4tTcYtUiNutKng_G8y0zxTNDk_bvbO_LJmAqrjA
```

Body:
```
{"name":"Ozone",
"definition":"https://en.wikipedia.org/wiki/Ozone",
"description":"Ozone is an inorganic molecule with the chemical formula O3. It is a pale blue gas with a distinctively pungent smell. It is an allotrope of oxygen that is much less stable than the diatomic allotrope O2, breaking down in the lower atmosphere to O2 or dioxygen. Ozone is formed from dioxygen by the action of ultraviolet light and also atmospheric electrical discharges, and is present in low concentrations throughout the Earth's atmosphere (stratosphere). In total, ozone makes up only 0.6 ppm of the atmosphere."}
```

## Help

- brian.miles@cgifederal.com
