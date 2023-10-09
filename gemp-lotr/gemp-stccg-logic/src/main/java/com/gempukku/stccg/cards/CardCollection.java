package com.gempukku.stccg.cards;

import java.util.Map;
import java.util.Objects;

public interface CardCollection {
    int getCurrency();

    Iterable<Item> getAll();

    int getItemCount(String blueprintId);

    Map<String, Object> getExtraInformation();

    class Item implements CardItem {
        public enum Type {
            PACK, CARD, SELECTION
        }

        private final Type _type;
        private final int _count;
        private final String _blueprintId;
        private final boolean _recursive;

        private Item(Type type, int count, String blueprintId, boolean recursive) {
            _type = type;
            _count = count;
            _blueprintId = blueprintId;
            _recursive = recursive;
        }

        public static Item createItem(String blueprintId, int count) {
            return createItem(blueprintId, count, false);
        }

        public static Item createItem(String blueprintId, int count, boolean recursive) {
            if (blueprintId.startsWith("(S)"))
                return new Item(Type.SELECTION, count, blueprintId, recursive);
            else if (!blueprintId.contains("_"))
                return new Item(Item.Type.PACK, count, blueprintId, recursive);
            else
                return new Item(Item.Type.CARD, count, blueprintId, recursive);
        }

        public static Item createItem(String combined) {
            String[] result = combined.split("x", 2);
            return createItem(result[1], Integer.parseInt(result[0]));
        }

        public Type getType() {
            return _type;
        }

        public int getCount() {
            return _count;
        }

        @Override
        public String getBlueprintId() {
            return _blueprintId;
        }

        @Override
        public boolean isRecursive() {
            return _recursive;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Item item = (Item) o;

            if (_count != item._count)
                return false;
            if (!Objects.equals(_blueprintId, item._blueprintId))
                return false;
            return _type == item._type;
        }

        @Override
        public int hashCode() {
            int result = _type != null ? _type.hashCode() : 0;
            result = 31 * result + _count;
            result = 31 * result + (_blueprintId != null ? _blueprintId.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return _count + "x" + _blueprintId;
        }
    }
}
