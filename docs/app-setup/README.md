# App Setup

This details the set up procedure within the app itself.

Before starting this, ensure the app is installed on the phone, see
[App Installation](../app-install/README.md), and the remote logging system is setup (If being 
used), see [Remote Logging](../remote-logging/README.md), and finally, the physical environment
is setup, see [Physical Setup](../physical-setup/README.md).

## Battery Optimisations *(Off-site)*

This step is optional but recommended to increase the lifespan of the phone.

Go to the phone's settings app and disable any battery intensive settings, such as the following.

 * Location
 * WiFi
 * Mobile Data
 * Bluetooth
 * Automatic Brightness
 
If using a remote logging system, you will need to enable WiFi or Mobile Data for the measurements
to be sent.

Many phones also have a battery saver mode that can be turned on.

You can also uninstall or disable any apps on the phone that may use battery in the background, 
eg. messaging or email apps that use the internet.

## Configuration Values *(Off-site)*

From the previous set up procedures, there will have been many configuration values recorded. These
values were marked with the '**📝 Configuration Value**' marker.

If you are using a [remote logging](../remote-logging/README.md) system, you need the following 
values from InfluxDB:

 * URL
 * Authorization Token
 * Organisation Name/ID
 * Bucket Name/ID
 
From the [physical setup](../physical-setup/README.md), you will need the following values:

 * Target size (m)
 * Initial distance (m)

Optionally, you can choose to change the target finding parameters from their default values. If
so, have the desired values recorded.
 
You will also need a device ID, this can be any bit of text describing the device and what it is
measuring, to separate it from other measurements.

Lastly, you will need to provide the measurement period that measurements will be taken in (In
minutes).

If you have large values such as the remote logging Authorization token, it is recommended
that you set up a way to get these values to the phone so that you can copy & paste them into the
app during app setup, eg. Texting to the phone.

## In-App Setup

Open the Displacement Monitoring app.

### Setup Screens

When the app is first opened you will be greeted with a series of setup screens.

#### Obtain Permissions *(Off-site)*

This screen will have you give the app consent to use the Android features that it requires.
Go through the list and tick all the boxes, and press next when done.

#### Configure Settings *(Off-site)*

This screen will have you go to the app's settings page and input all of the configuration
values you previously compiled. Once all are input, leave the settings page and press next.

#### Calibration *(On-site)*

This screen is for the important step of measuring a 'focal length', that the app uses to take it's
measurements.

Enter the calibration page and you should see a real-time view from the camera, with
the target outlined if it is found. If the wrong camera is being shown or it is rotated/warped in
a strange way, visit the settings page to fix this with the 'Warp Camera' and 'Select Camera'
settings.

Once everything looks right, ensure that the phone camera and the target are at the initial
distance, and the target is being highlighted in the camera view, and press calibrate. Once a
focal length value is measured you can leave the calibration page and press 'finish'.

### App Dashboard *(On-site)*

Once the initial setup screens are done, you will be taken to a dashboard. This is where you can
do the final step of scheduling measurements, but first there are some optional tests you may want
to perform beforehand.

#### Real-Time Measurement Test

This shows a real-time view of the camera with the target highlighted and the measured distance.
Use this to make sure the target is being found and the measurements line up with the real world.
If not, re-calibrate or adjust other settings.

#### Single Measurement Test

This takes a single measurement using the same behaviour as if the scheduled measurement period had
elapsed. Use this to test that the device can take a measurement and send it to the remote database.
If not, ensure the remote database settings are correct and the device has internet connection.

#### Finally - Schedule Measurements

Optional Battery Optimisation - Turn the screen brightness as low as possible before starting.

Once you are confident with everything, press the 'Start' button in 'Scheduling', and press
'Lock device now' to switch the screen off.

You're done, the phone will now turn on whenever the period has elapsed and take a measurement.
