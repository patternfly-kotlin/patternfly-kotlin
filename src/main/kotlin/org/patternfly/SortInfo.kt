package org.patternfly

/**
 * Simple class to hold information when sorting [Items]. Please note that the comparator is never reversed! It's only reversed 'in-place' when [ascending] == `false`
 *
 * @param id unique identifier
 * @param text text used in the UI
 * @param comparator comparator for this sort info
 * @param ascending whether the comparator is ascending or descending
 */
public class SortInfo<T>(
    public val id: String,
    public val text: String,
    internal val comparator: Comparator<T>,
    public val ascending: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SortInfo<*>
        if (id != other.id) return false
        if (ascending != other.ascending) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + ascending.hashCode()
        return result
    }

    override fun toString(): String {
        return "SortInfo(id=$id, ascending=$ascending)"
    }

    internal fun toggle(): SortInfo<T> = SortInfo(id, text, comparator, !ascending)

    internal fun effectiveComparator(): Comparator<T> = if (ascending) comparator else comparator.reversed()
}
