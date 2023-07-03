INSERT INTO public.match (id, name, si_match_id, content_id, language_id, tournament_id, season_id, start_time,
                          end_time, status, version, created_at, updated_at, deleted, state,
                          created_by, updated_by)
VALUES (1402, 'Thalaivas vs Steelers', '600160', '1440002191', 3, 76, 634, '2019-09-14 14:40:00.000000',
        '2099-12-31 18:29:00.000000',
        'UNPUBLISHED', '2020-03-17 05:08:54.000000', '2019-09-12 12:32:35.658000', '2019-09-14 17:02:52.854000', false,
        'COMPLETED', 'system',
        'system');

INSERT INTO public.match_break (id, content_id, breaks_starting, breaks_consumed, breaks_left, created_at, updated_at,
                                created_by, updated_by)
VALUES (1402, '1440002191', 10, 55, 10, '2019-09-12 12:32:35.653000', '2019-09-12 12:32:35.653000', 'system', 'system');

