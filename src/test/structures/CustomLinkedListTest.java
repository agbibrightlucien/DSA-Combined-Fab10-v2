package structures;

import org.junit.jupiter.api.Test;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

/** Part 13 correctness evidence for CustomLinkedList (Part 3). */
class CustomLinkedListTest {

    @Test
    void normal_addFirstAddLastAndIterate_returnsCorrectOrder() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.addLast("b");
        list.addLast("c");
        list.addFirst("a");

        CustomIterator<String> it = list.customIterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) sb.append(it.next());
        assertEquals("abc", sb.toString());
        assertEquals(3, list.size());
    }

    @Test
    void boundary_singleElementList_removeFirstEmptiesList() {
        CustomLinkedList<Integer> list = new CustomLinkedList<>();
        list.addFirst(42);
        assertEquals(42, list.removeFirst());
        assertTrue(list.isEmpty());
    }

    @Test
    void invalid_removeFirstOnEmptyList_throwsNoSuchElement() {
        CustomLinkedList<Integer> list = new CustomLinkedList<>();
        assertThrows(NoSuchElementException.class, list::removeFirst);
    }
}
