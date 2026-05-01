import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class MainTest {
    @Test
    void shouldReturnCorrectIndices() {
        Solution s = new Solution();
        int[] result = s.twoSum(new int[] {2, 7, 11, 15}, 9);
        assertArrayEquals(new int[] {0, 1}, result);
    }
}
