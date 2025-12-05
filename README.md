The change done versus the official sample is to add a button "change configuration" that enable the check digit option

# Video reproduction

The issue: programaticaly we can not enable/disable the check digit

[The video](https://github.com/DevHugo/ReproScannerSunmi/blob/master/repro_case.mov)

In the video:
  1. I opened the setting app showing that no symbologies are enabled, and the check digit option is disabled.
  2. I open the demo app click on the button "change configuration" that is supposed to enabled symbologies and check digit
  3. Change to the setting app, see it does not work
  4. Assert it does not work by scanning a barcode and see that the check digit is not enabled


