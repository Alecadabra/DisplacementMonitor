# Remote logging measurements

The app supports logging to a remote [InfluxDB](https://www.influxdata.com/) database. The
included [Dashboard](dashboard.json) can be imported into a [Grafana](https://grafana.com/) server 
to help visualise the incoming data remotely.

This setup can be skipped if remote logging is not required.
 
This process will generate values that are needed for the setup of the phone app. It is recommended
that you set up a way to get these values to the phone so that you can copy & paste them into the
app during app setup, eg. Texting to the phone.

> **Configuration Value**: Notes like this will show where there is a value that needs to be
> recorded for setup of the phone app. 

## Setting up a remote test environment

Included in this directory is a [docker-compose.yml](docker-compose.yml) file that can be used to 
quickly setup a test deployment of both InfluxDB and Grafana.

After installing Docker, navigate to the directory containing the `docker-compose.yml` file and 
run the following command:

```bash
docker-compose up -d --abort-on-container-exit
```

By default, Grafana will be accessible on port 80 and InfluxDB will be accessible on port 8080.

## Configure InfluxDB

A small amount of setup is required before it's possible to start logging data. Use a web browser 
to navigate to the InfluxDB web interface (http://localhost:8080/).

You will be prompted to fill out a username/password, an Organisation and a Bucket. All of these 
values are arbitrary and can be set to whatever you like. Naming the bucket something descriptive 
is always useful (i.e. `measurements`).

> **Configuration Value**: Record the exact names or IDs of the Organisation and Bucket so that they
>can be used when setting up the phone app. 

After setting up the initial account details, skip the rest of the configuration (Configure Later).

### Generate access tokens for InfluxDB

In order for Grafana and the mobile application to be able to read/write to InfluxDB they will 
need to authenticate using access tokens. Access tokens can be created in the InfluxDB web 
interface, we will create two access tokens; one for each application.

In the InfluxDB web interface, navigate to to `Data > Tokens` using the left navigation menu. 
Press the `Generate` button and select `Read/Write Token`. The first token will be for Grafana;
give the token an appropriate description (i.e. 'read-only Grafana token') and set the `Read` 
permissions to 'All Buckets'. Leave the `Write` permissions alone and create the token.

Repeat the process for the mobile app token, but instead leave the `Read` permissions alone and
select your measurements bucket in the `Write` permissions.

> **Configuration Value**: Record this mobile app token so it can be used when setting up the phone
>app.

## Configure Grafana

Use a web browser to navigate to the Grafana web interface (http://localhost/).
The initial login details for Grafana are simply admin/admin, you will be prompted to change 
the password on first login.

After logging in, you will need to add the InfluxDB container as a data source. You can do this 
by clicking on the Gear icon on in the left menu and navigating to the Data Sources view. Press 
the `Add data source` button to begin.

Select `InfluxDB` as the type, give it an appropriate name and select `Flux` as the query language
of choice. For the URL we will use the internal docker hostname and port for the InfluxDB container.
You can also use the host machine's IP address and publicly mapped port (8080) if you would like.

```
http://dmon_influx:8086/
```

> **Configuration Value**: Record the URL you choose to use so it can be used when setting up the
>phone app.

Leave the rest of the options as default (ensure that `Basic Auth` is turned OFF). 
Fill out the organisation/token/default bucket values with the appropriate values used when 
configuring InfluxDB (make sure to use the right token).

Hit Save and Test to ensure that the connection works correctly.

### Import the dashboard

After creating the Dashboard it should be a straightforward process to import the provided
[Dashboard](dashboard.json). Press the large `+` sign in left menu and choose `Import`. Either 
upload the dashboard.json file or copy its contents into the `Import via panel json` textbox.

To complete the import you will need to select the new Data Source and specify which bucket holds
the measurements data (the one you created earlier).
