package com.brandongogetap.stickyheaders.demo;

import com.brandongogetap.stickyheaders.exposed.StickyHeader;

class HeaderItem extends Item implements StickyHeader {

    HeaderItem(String title, String message) {
        super(title, message);
    }
}
