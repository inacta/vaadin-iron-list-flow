/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.ui.iron.list.it;

import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonNull;
import elemental.json.JsonObject;

@TestPath("iron-list-test")
public class IronListIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("iron-list")).size() > 0);
    }

    @Test
    public void listWithStrings() {
        String listId = "list-with-strings";

        testInitialLoadOfItems(listId, "Item ");
        clickToSet2Items_listIstUpdated(listId, "list-with-strings-2-items",
                "Another item ");
        clickToSet3Items_listIsUpdated(listId, "list-with-strings-3-items",
                "Item ");
        clickToSet0Items_listIsUpdated(listId, "list-with-strings-0-items");
    }

    @Test
    public void dataProviderWithStrings() {
        String listId = "dataprovider-with-strings";

        testInitialLoadOfItems(listId, "Item ");
        clickToSet2Items_listIstUpdated(listId,
                "dataprovider-with-strings-2-items", "Another item ");
        clickToSet3Items_listIsUpdated(listId,
                "dataprovider-with-strings-3-items", "Item ");
        clickToSet0Items_listIsUpdated(listId,
                "dataprovider-with-strings-0-items");
    }

    @Test
    public void templateFromValueProviderWithPeople() {
        String listId = "dataprovider-with-people";

        testInitialLoadOfItems(listId, "Person ");
        clickToSet2Items_listIstUpdated(listId,
                "dataprovider-with-people-2-items", "");
        clickToSet3Items_listIsUpdated(listId,
                "dataprovider-with-people-3-items", "Person ");
        clickToSet0Items_listIsUpdated(listId,
                "dataprovider-with-people-0-items");
    }

    @Test
    public void templateFromRendererWithPeople() {
        WebElement list = findElement(By.id("template-renderer-with-people"));

        JsonArray items = getItems(list);
        Assert.assertEquals(3, items.length());
        for (int i = 0; i < items.length(); i++) {
            Assert.assertEquals(String.valueOf(i + 1),
                    items.getObject(i).getString("key"));
            Assert.assertEquals("Person " + (i + 1),
                    items.getObject(i).getString("name"));
            Assert.assertEquals(String.valueOf(i + 1),
                    items.getObject(i).getString("age"));
            Assert.assertEquals("person_" + (i + 1),
                    items.getObject(i).getString("user"));
        }

        WebElement update = findElement(
                By.id("template-renderer-with-people-update-item"));

        scrollIntoViewAndClick(update);
        items = getItems(list);
        JsonObject person = items.getObject(0);
        Assert.assertEquals("Person 1 Updated", person.getString("name"));
        Assert.assertEquals("person_1_updated", person.getString("user"));
    }

    @Test
    public void lazyLoaded() {
        WebElement list = findElement(By.id("lazy-loaded"));
        WebElement message = findElement(By.id("lazy-loaded-message"));

        JsonArray items = getItems(list);
        // the items are preallocated in the list, but they are empty
        Assert.assertEquals(100, items.length());

        // default initial request for this size of list (100px height)
        Assert.assertEquals("Sent 32 items", message.getText());

        assertItemsArePresent(items, 0, 32, "Item ");

        // all the remaining items should be empty
        for (int i = 32; i < items.length(); i++) {
            Assert.assertThat(items.get(i),
                    CoreMatchers.instanceOf(JsonNull.class));
        }

        // scrolls all the way down
        executeScript("arguments[0].scrollBy(0,10000);", list);
        waitUntil(driver -> getItems(list).get(0) instanceof JsonNull);

        items = getItems(list);

        // all the initial items should be empty
        assertItemsAreNotPresent(items, 0, items.length() - 32);

        // the last 32 items should have data
        assertItemsArePresent(items, items.length() - 32, items.length(),
                "Item ");
    }

    private void assertItemsArePresent(JsonArray items, int startingIndex,
            int endingIndex, String itemLabelprefix) {

        for (int i = startingIndex; i < endingIndex; i++) {
            Assert.assertThat(
                    "Object at index " + i + " is null, when it shouldn't be",
                    items.get(i),
                    CoreMatchers.not(CoreMatchers.instanceOf(JsonNull.class)));
            Assert.assertEquals(itemLabelprefix + (i + 1),
                    items.getObject(i).getString("label"));
        }
    }

    private void assertItemsAreNotPresent(JsonArray items, int startingIndex,
            int endingIndex) {

        for (int i = startingIndex; i < endingIndex; i++) {
            Assert.assertThat(
                    "Object at index " + i + " is not null, when it should be",
                    items.get(i), CoreMatchers.instanceOf(JsonNull.class));
        }
    }

    private void testInitialLoadOfItems(String listId,
            String itemLabelPrefixForFirstSet) {
        WebElement list = findElement(By.id(listId));

        JsonArray items = getItems(list);
        Assert.assertEquals(3, items.length());

        assertItemsArePresent(items, 0, 3, itemLabelPrefixForFirstSet);
    }

    private void clickToSet2Items_listIstUpdated(String listId,
            String buttonIdFor2Items, String itemLabelPrefixForSecondSet) {
        WebElement list = findElement(By.id(listId));

        WebElement set2Items = findElement(By.id(buttonIdFor2Items));

        scrollIntoViewAndClick(set2Items);
        waitUntil(driver -> getItems(list).length() == 2);
        JsonArray items = getItems(list);
        for (int i = 0; i < items.length(); i++) {
            Assert.assertEquals(
                    "The label of the initial object at the index " + i
                            + " of the list '" + listId + "' is wrong",
                    itemLabelPrefixForSecondSet + (i + 1),
                    items.getObject(i).getString("label"));
        }
    }

    private void clickToSet3Items_listIsUpdated(String listId,
            String buttonIdFor3Items, String itemLabelPrefixForFirstSet) {
        WebElement list = findElement(By.id(listId));

        WebElement set3Items = findElement(By.id(buttonIdFor3Items));

        scrollIntoViewAndClick(set3Items);
        waitUntil(driver -> getItems(list).length() == 3);
        JsonArray items = getItems(list);
        for (int i = 0; i < items.length(); i++) {
            Assert.assertEquals(
                    "The label of the updated object at the index " + i
                            + " of the list '" + listId + "' is wrong",
                    itemLabelPrefixForFirstSet + (i + 1),
                    items.getObject(i).getString("label"));
        }
    }

    private void clickToSet0Items_listIsUpdated(String listId,
            String buttonIdFor0Items) {

        WebElement list = findElement(By.id(listId));

        WebElement set0Items = findElement(By.id(buttonIdFor0Items));
        scrollIntoViewAndClick(set0Items);
        waitUntil(driver -> getItems(list).length() == 0);
    }

    private JsonArray getItems(WebElement element) {
        Object result = executeScript("return arguments[0].items;", element);
        JsonArray array = Json.createArray();
        if (!(result instanceof List)) {
            return array;
        }
        List<Map<String, ?>> list = (List<Map<String, ?>>) result;
        for (int i = 0; i < list.size(); i++) {
            Map<String, ?> map = list.get(i);
            if (map != null) {
                JsonObject obj = Json.createObject();
                map.entrySet().forEach(entry -> {
                    obj.put(entry.getKey(), String.valueOf(entry.getValue()));
                });
                array.set(i, obj);
            } else {
                array.set(i, Json.createNull());
            }
        }
        return array;
    }

}
