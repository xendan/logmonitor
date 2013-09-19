package org.xendan.logmonitor.idea;

import com.intellij.ui.components.JBList;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.xendan.logmonitor.model.BaseObject;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

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
    @SuppressWarnings("unchecked")
    public void test_edit_create_remove() throws Exception {
        TestBean b1 = new TestBean();
        String name = "AAA";
        b1.setId(1L);
        b1.setName(name);
        TestBean b2 = new TestBean();
        b1.setId(2L);
        b2.setName("BBB");
        itemPanel.add(nameField);
        ValueModel listValue = new ValueHolder(Arrays.asList(b1, b2));
        TestSetItemFromListMode model = new TestSetItemFromListMode(newButton, removeButton, itemPanel, itemsList, listValue);
        assertFalse("Nothing to remove", removeButton.isEnabled());
        assertFalse("initial state is disabled", nameField.isEnabled());
        assertEquals(2, ((List<TestBean>) listValue.getValue()).size());

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
        assertEquals(3, itemsList.getModel().getSize());
        String newBean = "This is new bean";
        nameField.setText(newBean);

        assertEquals(newBean, ((TestBean)itemsList.getModel().getElementAt(2)).getName());
        assertTrue(nameField.isEnabled());

        //remove
        itemsList.setSelectedValue(b2, false);
        removeButton.doClick();
        assertEquals(2, itemsList.getModel().getSize());
        assertFalse(nameField.isEnabled());

        //add invalid
        newButton.doClick();
        itemsList.setSelectedValue(b1, false);
        assertEquals("Expect new invalid bean not added", 2, itemsList.getModel().getSize());
    }

    private class TestSetItemFromListMode extends SetItemFromListModel<TestBean> {


        public TestSetItemFromListMode(JButton newButton, JButton removeButton, JPanel itemPanel, JList itemsList, ValueModel valueModel) {
            super(newButton, removeButton, itemPanel, itemsList, valueModel, "name");
        }

        @Override
        protected void bind(VerboseBeanAdapter<TestBean> beanAdapter) {
            Bindings.bind(nameField, beanAdapter.getPropertyModel("name"));
        }

        @Override
        protected boolean isInvalid(TestBean item) {
            return StringUtils.isEmpty(item.getName());
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            TestBean testBean = (TestBean) o;

            return !(name != null ? !name.equals(testBean.name) : testBean.name != null);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }
}
