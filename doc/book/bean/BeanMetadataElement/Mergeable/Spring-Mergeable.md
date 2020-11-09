# Spring Mergeable
- 类全路径: `org.springframework.beans.Mergeable`
- 接口方法
    1. 是否需要合并
    2. 合并


```java
public interface Mergeable {

	/**
	 * Is merging enabled for this particular instance?
	 * 是否需要合并
	 */
	boolean isMergeEnabled();

	/**
	 * Merge the current value set with that of the supplied object.
	 * <p>The supplied object is considered the parent, and values in
	 * the callee's value set must override those of the supplied object.
	 *
	 * 合并方法
	 * @param parent the object to merge with
	 * @return the result of the merge operation
	 * @throws IllegalArgumentException if the supplied parent is {@code null}
	 * @throws IllegalStateException if merging is not enabled for this instance
	 * (i.e. {@code mergeEnabled} equals {@code false}).
	 */
	Object merge(@Nullable Object parent);

}

```

- 合并方法其实可以简单想一想,就可以大概知道有哪些. 例如: List 的 addAll, Map 的 putAll , Set 的 addAll . 通过类图和源码阅读Spring也确实是调用了这些方法来进行合并. 