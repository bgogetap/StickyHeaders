[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-StickyHeaders-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3922)
# StickyHeaders
Easily add Sticky Headers to your RecyclerView

### Setup
Implement `StickyHeaderHandler` in your Presenter/Adapter/Activity or whatever class has access to your RecyclerView adapter dataset

Make sure the parent of the RecyclerView is a FrameLayout or CoordinatorLayout (this will be verified at runtime). To avoid this runtime check, you can call `StickyLayoutManager#disableParentViewRestrictions`. This *will* cause bugs with some `ViewParent`s, such as `LinearLayout`, so use with caution.

Instantiate a `StickyLayoutManager` and set that as the LayoutManager for your RecyclerView.

For items in your dataset that you want to act as sticky headers, implement the marker interface `StickyHeader`.

That's it! See the example app for more in depth details.

#### Additional Features
Add elevation to your headers (animated in and out) on Lollipop and above:

`layoutManager.elevateHeaders(true)` OR `layoutManager.setElevation(int dp)`

Add a listener to be notified when headers are attached/re-bound or detached:

`StickyLayoutManager#setStickyHeaderListener`

You will be passed the instance of the view that was either attached/re-bound or detached, as well as the adapter position of the data that view represents. It is important to note that the adapter position passed to `headerDetached` may not be the current position in the data set that the view was originally bound with. This can happen if the data has changed since that header was made sticky.

The adapter position passed in `headerAttached` will always be correct at that moment.

![StickyHeaders](art/demo-padding.gif)

Add to your Gradle dependencies (Check badge at top for latest version):


```groovy
dependencies {
    implementation 'com.brandongogetap:stickyheaders:0.6.2'
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
