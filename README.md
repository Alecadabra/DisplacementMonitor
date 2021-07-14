# Displacement Monitor

A displacement measurement and monitoring Android app.

This app is made to be set up to monitor the displacement (distance) across a space, where the
phone is placed on one side, and a visual target is placed on the other side. The app periodically
measures the distance between the phone and the target, and can be set up to send these measurements
to a remote database and dashboard.

The app is written in Kotlin, roughly following the MVC pattern. OpenCV 3 is used to find and
measure the target, and InfluxDB is used as the remote database with a Grafana frontend.

## Contents

- [Guides](#guides)
  * [Installation](#installation)
  * [Remote Logging](#remote-logging)
  * [Physical Setup](#physical-setup)
  * [App Setup](#app-setup)
- [Screenshots](#screenshots)
- [Contributing](#contributing)
- [License](#license)

## Guides

### Installation

See [App Installation](./docs/app-install/README.md) for a guide on installing the APK onto
an Android phone.

### Remote Logging

If you wish to set up a remote logging system, see [Remote Logging](./docs/remote-logging/README.md)
for a guide on setting up the server, including an InfluxDB database and Grafana frontend.

### Physical Setup

For a guide on setting up the phone and target where diplacement is to be monitored, see 
[Physical Setup](./docs/physical-setup/README.md).

### App Setup

For a guide on initial configuration of the app and the set up procedure, see 
[App Setup](./docs/app-setup/README.md).

## Screenshots

Setup Page 1 | Setup Page 2 | Setup Page 3
--- | --- | ---
![Setup Page 1](https://user-images.githubusercontent.com/27219575/125593284-a6497528-a2bf-4f30-9fb0-1e4b2a1591a3.png) | ![Setup Page 2](https://user-images.githubusercontent.com/27219575/125593344-6b7dce68-0a76-43a4-a76f-0f8b2ba2585c.png) | ![Setup Page 3](https://user-images.githubusercontent.com/27219575/125593528-7bc8c2da-ee04-47be-bd73-945f8c14f6cc.png)

Home Page | Settings | Calibration
--- | --- | ---
![Home Page](https://user-images.githubusercontent.com/27219575/125593698-959e92f1-4dbb-4aa1-8e41-28898b2a891f.png) | ![Settings](https://user-images.githubusercontent.com/27219575/125593776-058d283c-fb69-4690-815c-c772d87c0c72.png) | ![Calibration](https://user-images.githubusercontent.com/27219575/125593821-7e0cd7d6-ffe3-4dd9-babc-e80e77876ada.png)

Real-Time Measurement | Measurements (Local) | Measurements (Remote - Grafana)
--- | --- | ---
![Real-Time Measurement](https://user-images.githubusercontent.com/27219575/125593876-563d8f9d-732b-418d-8171-2bfdc8b31b5e.png) | ![Measurements (Local)](https://user-images.githubusercontent.com/27219575/125593954-e5ba8bfb-d191-4121-aae3-34175d98a0a2.png) | ![Measurements (Remote - Grafana)](https://user-images.githubusercontent.com/27219575/125594239-db680201-2a0e-4d38-991e-abe28b7244ee.png)

## Contributing

This project is not under active development but pull requests are welcome.

## License

```
Copyright 2021 Alec Maughan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
