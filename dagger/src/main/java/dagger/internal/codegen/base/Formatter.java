package dagger.internal.codegen.base;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import static com.google.common.base.Preconditions.checkElementIndex;

/**
 * A formatter which transforms an instance of a particular type into a string
 * representation.
 * <p>
 * 将特定类型的实例转换为字符串表示形式的格式化程序。
 * <p>
 * 子类继承者会继承format实现自己的业务逻辑
 *
 * @param <T> the type of the object to be transformed.
 */
public abstract class Formatter<T> implements Function<T, String> {
    //一个缩进，4个字符
    public static final String INDENT = "    ";
    public static final String DOUBLE_INDENT = INDENT + INDENT;
    private static final int LIST_LIMIT = 10;


    /**
     * Performs the transformation of an object into a string representation.
     * <p>
     * T转换成String,子类实现具体业务逻辑
     */
    public abstract String format(T object);

    /**
     * Performs the transformation of an object into a string representation in conformity with the
     * {@link Function}{@code <T, String>} contract, delegating to {@link #format(Object)}.
     *
     * @deprecated Call {@link #format(Object)} instead. This method exists to make formatters easy to
     * use when functions are required, but shouldn't be called directly.
     */
    @SuppressWarnings("javadoc")
    @Deprecated
    @Override
    public final String apply(T object) {//function.apply方法调用格式处理方法
        return format(object);
    }

    /**
     * Formats {@code items}, one per line. Stops after {@value #LIST_LIMIT} items.
     */
    public void formatIndentedList(
            StringBuilder builder, Iterable<? extends T> items, int indentLevel) {

        //items最大不得超过10个，超过10不在for循环中处理，
        for (T item : Iterables.limit(items, LIST_LIMIT)) {
            String formatted = format(item);

            if (formatted.isEmpty()) {
                continue;
            }
            //换行
            builder.append('\n');
            //缩进
            appendIndent(builder, indentLevel);
            //加入核心逻辑format方法处理返回的字符串
            builder.append(formatted);
        }

        //超过10个意外的处理方法
        int numberOfOtherItems = Iterables.size(items) - LIST_LIMIT;
        if (numberOfOtherItems > 0) {
            builder.append('\n');
            appendIndent(builder, indentLevel);
            builder.append("and ").append(numberOfOtherItems).append(" other");
        }

        if (numberOfOtherItems > 1) {
            builder.append('s');
        }
    }

    //加几个缩进
    private void appendIndent(StringBuilder builder, int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            builder.append(INDENT);
        }
    }

    //返回的String类型可能是 "…,xxx,…"，index表示当前name所在位置，这里表示是在中间，我们假设在中间，前后都有其他参数
    public static String formatArgumentInList(int index, int size, CharSequence name) {
        checkElementIndex(index, size);
        StringBuilder builder = new StringBuilder();
        if (index > 0) {
            builder.append("…, ");
        }
        builder.append(name);
        if (index < size - 1) {
            builder.append(", …");
        }
        return builder.toString();
    }
}
