package co.id.bankbsi.superapp.testcodeasp.data.model

object ImageUrls {
    // Original endpoint requested by the assessment. Some IDs may return 404.
    fun primary(postId: Int) = "https://picsum.photos/id/$postId/200/200"

    // This fallback remains a network resource; the adapter has a final local drawable.
    fun secondary(postId: Int) =
        "https://placehold.co/200x200/DCE8FF/32456A/png?text=Post+$postId"
}
