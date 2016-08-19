# Capone Controller

[![Travis](https://travis-ci.org/capone-project/capone-core.svg)](https://travis-ci.org/capone-project/capone-controller)

Capone is a generic service framework which can be used to
connect different resources with each other. The overarching aim
is to have all network traffic completely authenticated and
secure, such that noone is able to perform unintended actions,
except people having the right to do so.

To achieve this goal, Capone uses capabilities. A capability is
bound to a certain user and grants him the right to perform a
single, clearly defined action. After a capability has been
issued, the person for whom the capability has been created may
present it to the service to then execute the desired action.

This project provides an application for the Android mobile
operating system. This application allows users to connect to
provided services as well as orchestrate services with each
other.

The project is licensed under the GPLv3 license. See the LICENSE
file or https://www.gnu.org/licenses/gpl-3.0.en.html for more
information.

## Building

The Capone Controller is implemented in Java, using Gradle as its
build system. To build the application, following dependencies
are required:

- Java Development Kit v8.0 or greater
- Android SDK Build-tools v24.0.1
- Android SDK Platform API v24

Building the project requires the following steps:

```
$ git clone --recursive https://github.com/capone-project/capone-controller.git
$ cd capone-controller
$ ./gradlew build
```

This will build the Capone Controller application.
