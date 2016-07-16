package com.brandongogetap.stickyheaders.demo;

import com.brandongogetap.stickyheaders.exposed.StickyHeader;

public final class HeaderItem extends Item implements StickyHeader {

    public HeaderItem(String title, String message) {
        super(title, message);
    }
}
