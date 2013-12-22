FWeather License Generator
==========================

###Prerequisites

The license generator requires to:

* install [NodeJS](http://nodejs.org/)
* execute `npm install grunt-cli -g` to globally install `grunt`
* execute `npm install` from the `/license` directory

###Generation of the license file

Steps for generating the `license.html` file:

1. `cd` to the `/license` directory.

2. Edit the `licenses.json` file and set the library information in the following format:
```json
  {
    "name": "Name of the library",
    "short": "shortname",
    "author": "The Author"
  }
```

3. Add the license file, `shortname.txt` in the `/license/files` directory. This file must contains all of the license
information for the library (e.g. `NOTICE` and `LICENSE`).

4. Execute `grunt`. The license file will be generated in the `/assets/www/` directory, so you don't have to do anything else.