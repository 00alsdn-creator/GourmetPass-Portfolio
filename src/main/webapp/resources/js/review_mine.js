/* src/main/webapp/resources/js/review_mine.js */

document.addEventListener("DOMContentLoaded", function() {
    const wrapper = document.querySelector('.review-mine-wrapper');
    if (!wrapper) return;

    const contextPath = wrapper.dataset.contextPath;

    /**
     * 페이징 클릭 이벤트 (이벤트 위임)
     */
    const pagination = document.querySelector('.pagination');
    if (pagination) {
        pagination.addEventListener('click', function(e) {
            const link = e.target.closest('.page-link');
            if (link) {
                e.preventDefault();
                const pageNum = link.dataset.page;
                if (pageNum) {
                    const href = link.getAttribute('href');
                    if (href && href !== '#') {
                        location.href = href;
                        return;
                    }
                    // 나의 이력 페이지로 페이지 번호만 들고 이동
                    location.href = contextPath + "/member/review/mine?pageNum=" + pageNum;
                }
            }
        });
    }

    /**
     * 리뷰 삭제 버튼 이벤트
     */
    wrapper.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-delete-review')) {
            const reviewId = e.target.dataset.reviewId;
            const storeId = e.target.dataset.storeId;
            
            // member_mypage.js에 정의된 공통 삭제 함수 호출
            if (typeof confirmDeleteReview === 'function') {
                confirmDeleteReview(reviewId, storeId);
            }
        }
    });
});
