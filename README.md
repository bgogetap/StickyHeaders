# StickyHeaders
Easily add Sticky Headers to your RecyclerView

###Setup
Implement `StickyHeaderHandler` in your Presenter/Activity or whatever class has access to your RecyclerView adapter dataset, as well as the ViewGroup hosting the RecyclerView.

Instantiate a `StickyLayoutManager` and set that as the LayoutManager for your RecyclerView.

For items in your dataset that you want to act as sticky headers, implement the marker interface `StickyHeader`.

That's it! See the example app for more in depth details.
