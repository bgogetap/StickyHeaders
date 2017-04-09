Change Log
==========
Version 0.4.3 *(2017-4-8)*
----------------------------
- Fix crash when adapter data set is null

Version 0.4.2 *(2017-3-27)*
----------------------------
- Remove `<application>` tag from library manifest.

Version 0.4.1 *(2017-3-27)*
----------------------------
- Clear attached header, if any, when a new `LayoutManager` is set on the `RecyclerView`.

Version 0.4.0 *(2016-12-10)*
----------------------------
- Add option to set a listener to notify when headers are attached or detached.

Version 0.3.4 *(2016-10-01)*
----------------------------
- Fixes a crash when current sticky header is removed from the adapter data set and `notifyDataSetChanged` is called

Version 0.2.0 *(2016-7-21)*
---------------------------
- StickyHeaderHandler now only needs to provide the dataset supplied to the RecyclerView.Adapter
- Sticky headers will have margins applied if the RecyclerView has padding.
  - If orientation is Vertical and there is top padding (or Horizontal and left padding), the RecyclerView must have `clipToPadding` set to `false`.
    - If you need top (or left if Horizontal) separation, use margin instead of padding on the RecyclerView.
