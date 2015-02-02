package inspector.jmondb.viewer.view.gui;

/*
 * #%L
 * jMonDB Viewer
 * %%
 * Copyright (C) 2014 InSPECtor
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.swing.*;
import java.util.Comparator;

/**
 *
 * Custom model to make sure the items are stored in a sorted order.
 * The default is to sort in the natural order of the item, but a Comparator can be used to customize the sort order.
 *
 * Adapted from: https://stackoverflow.com/a/17061439
 */
public class SortedComboBoxModel<E> extends DefaultComboBoxModel<E> {

    private Comparator comparator;

    /**
     * Create an empty model that will use the natural sort order of the item
     */
    public SortedComboBoxModel() {
        super();
    }

    /**
     * Create an empty model that will use the specified Comparator
     */
    public SortedComboBoxModel(Comparator comparator) {
        super();
        this.comparator = comparator;
    }

    @Override
    public void addElement(E element) {
        insertElementAt(element, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insertElementAt(E element, int index) {
        // determine where to insert element to keep model in sorted order
        int sortIndex;
        for(sortIndex = 0; sortIndex < getSize(); sortIndex++) {
            if(comparator != null) {
                E o = getElementAt(sortIndex);

                if(comparator.compare(o, element) > 0) {
                    break;
                }
            } else {
                Comparable c = (Comparable) getElementAt(sortIndex);

                if(c.compareTo(element) > 0) {
                    break;
                }
            }
        }

        super.insertElementAt(element, sortIndex);

        // select an element when it is added to the beginning of the model
        if(sortIndex == 0 && element != null) {
            setSelectedItem(element);
        }
    }
}
