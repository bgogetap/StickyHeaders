Change Log
==========

Version 0.2.0 *(2016-7-21)*
---------------------------
- StickyHeaderHandler now only needs to provide the dataset supplied to the RecyclerView.Adapter
- Sticky headers will have margins applied if the RecyclerView has margins.
  - If orientation is Vertical and there is a top margin (or Horizontal and left margin), the RecyclerView must have `clipToPadding` set to `false`.
    - If you need top (or left if Horizontal) separation, use margin instead of padding on the RecyclerView.
