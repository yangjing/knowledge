package example.quicksort

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-16.
  */
class QuickSortScala {

  /**
    * Scala版快速排序，泛型。函数式，可重入，线程安全，不改变原有序列值
    *
    * @param seqs 要排序序列
    * @return 排序后序列，排序后结果将生成一个新序列。
    */
  def quickSort[T](seqs: Seq[T])(implicit ev: T => Ordered[T]): Seq[T] = {
    if (seqs.isEmpty)
      seqs
    else {
      val elem = seqs.head
      quickSort(seqs.filter(_ < elem)) ++ (elem +: quickSort(seqs.filter(_ > elem)))
    }
  }

}
