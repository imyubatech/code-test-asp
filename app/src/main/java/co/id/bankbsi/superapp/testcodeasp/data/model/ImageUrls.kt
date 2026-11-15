package co.id.bankbsi.superapp.testcodeasp.data.model

object ImageUrls {
    fun primary(postId: Int) = "https://picsum.photos/id/$postId/200/200"

    fun secondary(postId: Int) =
        "https://placehold.co/200x200/DCE8FF/32456A/png?text=Post+$postId"
}
