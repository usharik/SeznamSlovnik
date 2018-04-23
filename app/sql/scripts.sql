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

select count(word_id) from CASES_OF_NOUN

select lang, count(id) from WORD
group by lang

select * from WORD order by id desc

select * from TRANSLATION order by id desc