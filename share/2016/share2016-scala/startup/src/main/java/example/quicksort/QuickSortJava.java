package example.quicksort;

/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-16.
 */
public class QuickSortJava {

    void setQuickSort(int[] array, int left, int right) {
        int i = left;
        int j = right;
        int key = array[left];
        int keyFlag = left;
        while (i < j) {
            while (i < j && key <= array[j]) {
                j--;
            }
            if (i < j) {
                int tmp = array[j];
                array[j] = array[i];
                array[i] = tmp;
                keyFlag = j;
            }
            while (i < j && key >= array[i]) {
                i++;
            }
            if (i < j) {
                int tmp1 = array[j];
                array[j] = array[i];
                array[i] = tmp1;
                keyFlag = i;
            }
        }
        if (keyFlag > left + 1) {
            setQuickSort(array, left, keyFlag - 1);
        }
        if (keyFlag < right - 1) {
            setQuickSort(array, keyFlag + 1, right);
        }
    }

}
