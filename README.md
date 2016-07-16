[ ![Download](https://api.bintray.com/packages/bgogetap/android/StickyHeaders/images/download.svg) ](https://bintray.com/bgogetap/android/StickyHeaders/_latestVersion) [![Build Status](https://travis-ci.org/bgogetap/StickyHeaders.svg?branch=tests)](https://travis-ci.org/bgogetap/StickyHeaders)
# StickyHeaders
Easily add Sticky Headers to your RecyclerView

###Setup
Implement `StickyHeaderHandler` in your Presenter/Activity or whatever class has access to your RecyclerView adapter dataset, as well as the ViewGroup hosting the RecyclerView.

Instantiate a `StickyLayoutManager` and set that as the LayoutManager for your RecyclerView.

For items in your dataset that you want to act as sticky headers, implement the marker interface `StickyHeader`.

That's it! See the example app for more in depth details.

![StickyHeaders](art/demo.gif)

Add to your Gradle dependencies (Check badge at top for latest version):

```groovy
buildscript {
    repositories {
        jcenter()
    }
}

dependencies {
    compile 'com.brandongogetap:stickyheaders:x.y.z'
}
```
