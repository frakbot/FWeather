FWeather
========
![Icon](/res/drawable-xhdpi/ic_launcher.png)

**FWeather** is a weather widget for Android based upon Tobias van Scheider's
[Authentic Weather](http://www.behance.net/gallery/Authentic-Weather/7196565) design.

This widget states the obvious and even invites you to look out of that bloody window
you've right in front of you.

Please be warned, this widget prominently uses cursing. If you don't like it,
just don't install the widget. The screenshots are censored, but the widget itself is not.

The permissions (coarse location, internet access) are requested to access your
coarse geolocation and to actually retrieve the weather at your location.
I don't collect any information, and as of version 1.0 your position is not even
stored locally. The app is open source, so you might just check out the code
if you don't trust my word! I've coded the widget in less than a day as a
learning experience for working with app widgets, so it's not really customizable
nor flexible (at the moment).

The widget supports [a few languages](#translating-fweather), but if you want
to contribute a translation you just have to issue a pull request
(remember to work on the develop branch!). Feedback is always welcome :)

You can also find FWeather **[on XDA-Developers](http://forum.xda-developers.com/showthread.php?t=2346105)**!


## Screenshots
![On the homescreen](http://hostr.co/file/JSS1T9zM56uG/2013-07-01-01.29.15_w400.png)&emsp;![On the lockscreen](http://hostr.co/file/AjKTBYCkSqPI/2013-07-01-01.35.53_w400.png)

## Download
You can download **FWeather** from the Google Play Store:

[![Google Play Store](https://developer.android.com/images/brand/en_generic_rgb_wo_60.png)](https://play.google.com/store/apps/details?id=net.frakbot.FWeather)

Please note that we're also running a **public beta** through the Google Play Store.
If you want to help us out and test new features, you can read the instructions
[in the wiki](https://github.com/frakbot/FWeather/wiki/Joining-the-beta).


## Translating FWeather
You can contribute to the FWeather translation in your own language.
It's really easy, just follow the intructions [in our wiki](https://github.com/frakbot/FWeather/wiki/Translating-FWeather)!

As of today, FWeather has been translated into the following languages:
* Chinese (Simplified) (by Hansi Liu)
* Czech (by Pavel Tížek)
* Dutch (by Frans-Peter van der Leur)
* English (by Frakbot)
* French (by Christian Knaub)
* German (by Aaron Gerlach)
* Greek (by Dins)
* Italian (by Frakbot)
* Romanian (by BruceLee)
* Russian (by Vsevolod Osinin)
* Spanish (by Anon)

These languages are in the works and should come in future updates:
* Hebrew (by Tomer Nachum)
* Portuguese Brazilian (by Edu Miguel)
* Ukrainian (by Ярослав Шевчук and Yegor Vyalov)

Please note that this list is preliminary and might change without any notice.

## Contributing and requesting features
Feel free to submit pull requests, we're more than open to your contributions!

If you want to send us a generic feedback, [write us an email](mailto:frakbot+fweather@gmail.com).
For bugs and feature requests, please read [the instructions](https://github.com/frakbot/FWeather/wiki/How-to-report-a-bug)
and then [open an issue](https://github.com/frakbot/FWeather/issues) in the issue tracker.

## Credits
This widget uses the awesome [Yahoo! Weather APIs](http://developer.yahoo.com/weather/)
to retrieve the weather data.

FWeather also uses the following libraries and open source projects:

* **[ActionBarSherlock](http://actionbarsherlock.com/)** by Jake Wharton
* **[WeatherApp](https://github.com/survivingwithandroid/Surviving-with-android/tree/master/WeatherApp)** by Surviving With Android
* **[Android Switch Backport](https://github.com/BoD/android-switch-backport)** by Benoit 'BoD' Lubek
* **[Dashclock](https://code.google.com/p/dashclock/)** by Roman Nurik (Google Inc.)
* **[Log Collector](https://code.google.com/p/android-log-collector/)** by Xtralogic Inc.

## License
This app's code is licensed under the Apache 2 license.
Please see the [NOTICE](/NOTICE) file for details.
