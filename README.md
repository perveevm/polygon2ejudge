# polygon2ejudge

Tool for automatic configuring Polygon problems on eJudge

## Build

Use `./gradlew build && ./gradlew buildJar` for building `.jar` file (located at `build/lib/polygon2ejudge-VERSION.jar`)

## Run

You should use such command to download and prepare contest from Polygon:

`java -jar polygon2ejudge-VERSION.jar --credentials <cred_path> --contest_id <ID> --config <conf_path> --contest_dir <path>`

* `cred_path` is a path to file with you Polygon API credentials. You can generate them in Polygon web interface. File should have `.xml` format and such content:

```xml
<?xml version="1.0" encoding="utf-8" ?>

<credentials>
	<key>PUT YOUR KEY HERE</key>
	<secret>PUT YOUR SECRET HERE</secret>
</credentials>
```

* `ID` is contest ID in Polygon.

* `conf_path` is a path to default config `serve.cfg`. There must be an abstract problem named `Generic`.

* `path` is a contest directory.

## Usage

1. Create empty contest in eJudge.
2. Run script using created contest directory.
3. Go to contest settings, commit changes and check contest settings.
4. Profit!

## Problems

1. ~~Only C++ checkers/generators/validators are supported~~ Done!
2. ~~No interactive problems support~~ Done!
3. ~~Only C++ main correct solutions supported~~ Done!
4. ~~No test validation~~ Done!
5. No flexible parameters configuration in serve.cfg
