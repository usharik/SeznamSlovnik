select *, date(load_date/1000, 'unixepoch', 'localtime')
  from WORD a
  left join WORD_TO_TRANSLATION b on A.id = b.word_id
 where b.id is null
 order by date(load_date/1000, 'unixepoch', 'localtime') desc

select date(load_date/1000, 'unixepoch', 'localtime'), count(id)
  from WORD
 group by date(load_date/1000, 'unixepoch', 'localtime')
 order by 1 desc

delete from WORD
 where id in (
    select a.id
      from WORD a
      left join WORD_TO_TRANSLATION b on A.id = b.word_id
     where b.id is null)

select count(*) from (
select distinct word_id from CASES_OF_NOUN
)

select lang, count(id) from WORD
group by lang

select * from WORD order by id desc

select * from TRANSLATION order by id desc

select A.translation
 from TRANSLATION as A
inner join WORD_TO_TRANSLATION as B on A.id = B.translation_id
inner join WORD as C on B.word_id = C.id
where C.word = 'byt'
  and C.lang = 'cz'
  and A.lang = 'ru'
order by A.translation
limit 100

select word_id, info from WORD_INFO
where info like 'rod:%'
  and word_id = 25447
order by word_id
--limit 1

select * from CASES_OF_NOUN where word_id = 25220