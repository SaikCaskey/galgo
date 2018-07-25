A fork of https://github.com/inaka/galgo to (initially) add some support for later APIs

Feel free to contribute to the original repo, my guess is that it wasn't intended as a production piece.

This is something I use for debugging prototypes etc, it's really handy


## Sample logging class 
```CustomLogger``` is a quick way to get started with this - it's pretty much what I'm using in my application currently (and, be aware, it's _far from perfect_)

I would recommend you avoid using Galgo in production due to the static instances for the logger and views, but it's not a big problem

You can easily customise Galgo, as you can see me do in ```showLogCatLogsOverlay```

## FYI
if you do use this, be aware that I might not maintain it

## Gradle
Include this in your app/ build.gradle dependencies: 

```implementation 'com.github.saikcaskey:systemecks-galgo:1.0.1'```

You may also need to add this to your top-level build.gradle file : 

```maven { url "http://dl.bintray.com/systemecks/system-ecks" }```

