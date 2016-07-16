[ ![Download](https://api.bintray.com/packages/bgogetap/android/StickyHeaders/images/download.svg) ](https://bintray.com/bgogetap/android/StickyHeaders/_latestVersion) [![Build Status](https://travis-ci.org/bgogetap/StickyHeaders.svg?branch=tests)](https://travis-ci.org/bgogetap/StickyHeaders)
# StickyHeaders
Easily add Sticky Headers to your RecyclerView

###Setup
Implement `StickyHeaderHandler` in your Presenter/Activity or whatever class has access to your RecyclerView adapter dataset, as well as the ViewGroup hosting the RecyclerView. (The parent of the RecyclerView must be a FrameLayout or CoordinatorLayout)

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

License
-------

    Copyright 2016 Brandon Gogetap

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
