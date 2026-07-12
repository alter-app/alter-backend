-- PostingKeyword 폐기: 업종(業種)은 이제 업장(Workspace)의 business_type FK 로 관리한다.
-- ⚠️ 파괴적: posting_keywords / posting_keyword_map 의 데이터가 영구 삭제된다.
-- FK 순서상 join 테이블(posting_keyword_map)을 먼저 제거한다.

DROP TABLE IF EXISTS posting_keyword_map;
DROP TABLE IF EXISTS posting_keywords;
