package me.ssttkkl.sharenote.ui.noteedit

class TagItemModels private constructor(private val list: List<TagItemModel<*>>) :
    List<TagItemModel<*>> by list {

    companion object {
        fun build(tags: List<String>): TagItemModels {
            return TagItemModels(
                tags.map { tag -> TagItemModel.Tag(tag) }
                    .plus(TagItemModel.AddTag)
            )
        }

        val EMPTY = build(emptyList())
    }

    fun rebuild(
        whichShowsRemove: String?
    ): TagItemModels {
        return TagItemModels(
            map { model ->
                when (model) {
                    is TagItemModel.Tag -> model.copy(showRemove = model.name == whichShowsRemove)
                    is TagItemModel.AddTag -> model
                }
            }
        )
    }

    operator fun plus(
        newTag: String
    ): TagItemModels {
        return TagItemModels(
            filterIsInstance<TagItemModel.Tag>()
                .plus(TagItemModel.Tag(newTag))
                .plus(TagItemModel.AddTag)
        )
    }

    operator fun minus(
        tag: String
    ): TagItemModels {
        return TagItemModels(
            filterIsInstance<TagItemModel.Tag>()
                .filter { it.name == tag }
                .plus(TagItemModel.AddTag)
        )
    }

    operator fun minus(
        tag: TagItemModel.Tag
    ): TagItemModels {
        return TagItemModels(list.minus(tag))
    }
}