package org.xendan.logmonitor.idea;

import com.intellij.ui.components.JBList;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.list.ArrayListModel;
import org.junit.Test;
import org.xendan.logmonitor.model.BaseObject;

import javax.swing.*;
import java.util.Arrays;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

/**
 * User: id967161
 * Date: 16/09/13
 */
public class SetItemFromListModelTest {

    private JTextField nameField = new JTextField();
    private JButton newButton = new JButton();
    private JButton removeButton = new JButton();
    private JPanel itemPanel = new JPanel();
    private JList itemsList = new JBList();

    @Test
    public void test_edit_create_remove() throws Exception {
        TestBean b1 = new TestBean();
        String name = "AAA";
        b1.setName(name);
        TestBean b2 = new TestBean();
        itemPanel.add(nameField);
        TestSetItemFromListMode model = new TestSetItemFromListMode();
        assertFalse("Nothing to remove", removeButton.isEnabled());
        assertFalse("initial state is disabled", nameField.isEnabled());
        ArrayListModel<TestBean> listModel = new ArrayListModel<TestBean>(Arrays.asList(b1, b2));
        model.setItemsModel(listModel);

        assertNull("Initially nothing is selected", model.getSelected());
        //edit b1
        itemsList.setSelectedValue(b1, false);
        assertEquals("Expect item selected in list", b1, model.getSelected());
        assertTrue("After value is selected, edit should be enabled", nameField.isEnabled());
        assertEquals(name, nameField.getText());
        String updateName = "Updated name";
        nameField.setText(updateName);
        assertEquals(updateName, b1.getName());

        //create new
        newButton.doClick();
        String newBean = "This is new bean";
        nameField.setText(newBean);

        assertEquals(newBean, listModel.get(2).getName());
        assertFalse(nameField.isEnabled());

        //remove
        itemsList.setSelectedValue(b2, false);
        removeButton.doClick();
        assertEquals(2, itemsList.getModel().getSize());
        assertFalse(nameField.isEnabled());
    }

    private class TestSetItemFromListMode extends SetItemFromListModel<TestBean> {
        public TestSetItemFromListMode() {
            super(newButton, removeButton, itemPanel, itemsList);
        }

        @Override
        protected void bind(BeanAdapter<TestBean> beanAdapter) {
            Bindings.bind(nameField, beanAdapter.getValueModel("name"));
        }
    }

    public static class TestBean extends BaseObject {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
