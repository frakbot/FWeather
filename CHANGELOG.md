Changelog
=========

### Version 2.2.1
Released on ???

- NEW: Greek translation -- thanks, Dins!

### Version 2.2.0
Released on 10th November 2013

- NEW: Czech translation -- thanks, Pavel Tížek!
- NEW: Dutch translation -- thanks, Frans-Peter van der Leur!
- NEW: French translation -- thanks, Christian Knaub!
- NEW: Russian translation -- thanks, Vsevolod Osinin!
- FIX: fixed the code that gives the translator attribution

### Version 2.1.5
Released on 14th October 2013

- FIX: solved crash caused by misbehaving Google Play Services

### Version 2.1.4
Released on 10th October 2013

- NEW: shaved some 200 bytes off the APK size (thanks XhmikosR)
- FIX: solved crash caused by `SharedPreferences` issues (issue #69)

### Version 2.1.3
Released on 8th October 2013

- NEW: updated ActionBarSherlock to v4.4.0
- NEW: updated Android Switch Backport
- NEW: losslessly compressed all the drawables to have a slightly lighter APK
- FIX: fixed feedback gathering being performed on the UI thread

### Version 2.1.2
Released on 7th October 2013

- FIX: added few missing weather conditions handling
- FIX: fixed NPE crash when saving null data in the permanent cache

### Version 2.1.1
Released on 5th October 2013

- NEW: weather data is now permanently cached throughout sessions to minimize "WTF" messages (cache expires after 2 hours)
- NEW: updated Play Services library code to v3.2.65 (was v3.1.36)
- FIX: no more "can't find location" errors when using manual location
- FIX: various fixes and improvements to location-handling code (should be much more robust in handling corner cases)

### Version 2.1.0
Released on 26th September 2013

- NEW: Spanish translation
- NEW: Romanian translation -- thanks, BruceLee!
- NEW: even better feedback mechanism
- FIX: crash on tablets when opening the Settings activity

### Version 2.0.1
Released on 11th September 2013

- FIX: updated Chinese translation -- thanks, Hansi Liu!
- FIX: updated German translation -- thanks, Aaron Gerlach!

### Version 2.0.0
Released on 3rd September 2013

- NEW: customizable weather location (current position, or manual)
- NEW: switched weather provider to Yahoo! Weather for better accuracy
- NEW: added a whole new set of weather statuses and messages
- NEW: added widget BG color preference
- NEW: added Dashclock and Log Collector to OSS license info
- NEW: differentiated error message for when no location is available
- NEW: vastly improved feedback mechanism (requires `READ_LOGS` and `WRITE_EXTERNAL_STORAGE` permissions)
- FIX: crash when sending feedback, if the Play Store doesn't allow it
- FIX: updated German translation -- thanks, Aaron Gerlach!
- FIX: various bugfixes

### Version 1.3.1
Released on 7th August 2013

- FIX: reduced widget text size for mdpi devices (which usually have crappy small screens)
- FIX: updated Chinese translation -- thanks, Hansi Liu!

### Version 1.3.0
Released on 5th August 2013

- NEW: Chinese translation -- thanks, Hansi Liu!

### Version 1.2.3
Released on 25th July 2013

- NEW: built against Android 4.3 SDK (API level 18)
- NEW: declares explicitly that it should only be installed on devices that support app widgets
- FIX: updated German language -- thanks, Aaron Gerlach!

### Version 1.2.2
Released on 24th July 2013

- NEW: added translator name in the About box
- FIX: updated German language -- thanks, Aaron Gerlach!
- FIX: clarified that the language forcing only influences the widget itself

### Version 1.2.1
Released on 20th July 2013

- NEW: added German language (still missing a couple strings) -- thanks, Aaron Gerlach!

### Version 1.1.4
Released on 11th July 2013

- FIX: crash on the Settings Activity on xlarge screens (multicolumn layout)

### Version 1.1.3
Released on 11th July 2013

- NEW: weather is now cached to reduce "???" messages while searching for a location
- NEW: you can now force the widget texts language (auto, English or Italian, for now)
- FIX: removed 1 minute refresh interval (was only meant for debugging)
- FIX: removed buttons from loading layouts, they couldn't do anything anyway
- FIX: removed minor bug when switching from loading layout to "real" layout (could have caused issues to poorly written launchers)

### Version 1.1-beta2
Released on 10th July 2013

- NEW: added the "Debug" setting
- NEW: completely rewritten logging system, should allow users to produce more meaningful log reports
- FIX: widget buttons would end up under the widget text when the weather icon was hidden
- FIX: some use cases require using the `ACCESS_FINE_LOCATION` permission which was not requested by the app (WiFi/Google location off, GPS on)
- FIX: opening the Authors dialog on Android 2.3 would crash the app
- FIX: minor UI fixes

### Version 1.1-beta1
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

### Version 1.0
Released on 30th June 2013

- First Play Store build
- Basic widget, no customization
