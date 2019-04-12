Change Log
==========
Version 0.6.0 *(2019-04-11)*
----------------------------
- AndroidX is now in master -- no need to specify `-androidx` in the version
- Fixed #72 (potential memory leak). Thanks @nkontizas!

Version 0.5.1 *(2018-06-13)*
----------------------------
- Fix AAPT2 build issue (thanks @armansimonyan13!)

Version 0.5.0 *(2017-12-18)*
----------------------------
- Header visibility now follows changes to the RecyclerView visibility
- If first item is a sticky header, it will not be stickied if the RecyclerView is scrolled to the top. This fixes an issue where the header would cover the edge effect if the RecyclerView had no top padding.

Version 0.4.9 *(2017-8-5)*
----------------------------
- Fix issues with headers not being cleared if removed from the data set.
- Add several UI tests to prevent future regressions.

**It is strongly recommended that users on previous versions, especially 0.4.5 - 0.4.8, upgrade to this version.** 

Version 0.4.4 *(2017-4-26)*
----------------------------
- Fix issue where a previously stickied header is not detached after adapter updated with a sticky header at position 0 which also happens to be the first visible position.
 
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
