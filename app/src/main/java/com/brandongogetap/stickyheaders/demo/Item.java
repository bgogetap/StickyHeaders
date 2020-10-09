package com.brandongogetap.stickyheaders.demo;

public class Item {

    final String title;
    final String message;

    public Item(String title, String message) {
        this.title = title;
        this.message = message;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (!title.equals(item.title)) return false;
        return message.equals(item.message);
    }

    @Override public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }
}
