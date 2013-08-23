Changelog
=========

### Version 2.0.0 ###
Released on ???

- NEW: switched weather provider to Yahoo! Weather for better accuracy
- NEW: added widget BG color preference
- NEW: added Dashclock to OSS license info
- NEW: differentiated error message for when no location is available
- FIX: fix for crash when sending feedback, if the Play Store doesn't allow it
- FIX: updated German translation -- thanks, Aaron Gerlach!

### Version 1.3.1 ###
Released on 7th August 2013

- FIX: reduced widget text size for mdpi devices (which usually have crappy small screens)
- FIX: updated Chinese translation -- thanks, Hansi Liu!

### Version 1.3.0 ###
Released on 5th August 2013

- NEW: Chinese translation -- thanks, Hansi Liu!

### Version 1.2.3 ###
Released on 25th July 2013

- NEW: built against Android 4.3 SDK (API level 18)
- NEW: declares explicitly that it should only be installed on devices that support app widgets
- FIX: updated German language -- thanks, Aaron Gerlach!

### Version 1.2.2 ###
Released on 24th July 2013

- NEW: added translator name in the About box
- FIX: updated German language -- thanks, Aaron Gerlach!
- FIX: clarified that the language forcing only influences the widget itself

### Version 1.2.1 ###
Released on 20th July 2013

- NEW: added German language (still missing a couple strings) -- thanks, Aaron Gerlach!

### Version 1.1.4 ###
Released on 11th July 2013

- FIX: crash on the Settings Activity on xlarge screens (multicolumn layout)

### Version 1.1.3 ###
Released on 11th July 2013

- NEW: weather is now cached to reduce "???" messages while searching for a location
- NEW: you can now force the widget texts language (auto, English or Italian, for now)
- FIX: removed 1 minute refresh interval (was only meant for debugging)
- FIX: removed buttons from loading layouts, they couldn't do anything anyway
- FIX: removed minor bug when switching from loading layout to "real" layout (could have caused issues to poorly written launchers)

### Version 1.1-beta2 ###
Released on 10th July 2013

- NEW: added the "Debug" setting
- NEW: completely rewritten logging system, should allow users to produce more meaningful log reports
- FIX: widget buttons would end up under the widget text when the weather icon was hidden
- FIX: some use cases require using the ACCESS_FINE_LOCATION permission which was not requested by the app (WiFi/Google location off, GPS on)
- FIX: opening the Authors dialog on Android 2.3 would crash the app
- FIX: minor UI fixes

### Version 1.1-beta1 ###
Released on 8th July 2013

- NEW: added Settings Activity
    - Customize widget
    - Dark mode (useful for bright background)
    - Set refresh rate and manually refresh
    - Licenses, credits, feedback mechanism
- NEW: added buttons on the widget to refresh and configure it
- NEW: use Google Play Services Location provider where available (better accuracy, low power consumption)
- NEW: use Google Analytics to get feedback on errors and issues and direct future development (with opt-out)
- FIXED: widget being stuck on "loading" ("Let me look out of a f*****g window")
- FIXED: bad handling of weather JSON when there is an error/malformation

### Version 1.0 ###
Released on 30th June 2013

- First Play Store build
- Basic widget, no customization
