-- 공고 직접입력 업종(라벨) 저장용 jsonb 컬럼 추가.
-- 마스터(posting_keywords)에 등록하지 않는 자유 입력 업종명 리스트를 공고에 직접 보관한다.
ALTER TABLE postings
    ADD COLUMN custom_keywords jsonb;
