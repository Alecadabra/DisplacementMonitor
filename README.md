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
