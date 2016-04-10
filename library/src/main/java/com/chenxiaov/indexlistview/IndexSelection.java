package com.chenxiaov.indexlistview;

import java.util.List;

/**
 * IndexView ListAdapter
 * Created by chenxv on 16/4/8.
 */
public interface IndexSelection {

    int getIndexByItem(int firstItem);

    int getSelectionByIndex(int index);

    List<String> indexValues();

    String title();

}
