-- Seed: 3 bookings
INSERT INTO booking (id, start_at, end_at, duration_hours, vehicle_id, status) VALUES
                                                                                   (
                                                                                       '11111111-1111-1111-1111-111111111111',
                                                                                       '2026-03-10 09:00:00',
                                                                                       '2026-03-10 11:00:00',
                                                                                       2,
                                                                                       '5b2b9322-3a9a-42b0-84a0-7d27af53dea4',
                                                                                       'ACTIVE'
                                                                                   ),
                                                                                   (
                                                                                       '22222222-2222-2222-2222-222222222222',
                                                                                       '2026-03-10 12:00:00',
                                                                                       '2026-03-10 14:00:00',
                                                                                       3,
                                                                                       '6c84d63c-cfb9-4088-933f-66c837794787',
                                                                                       'ACTIVE'
                                                                                   ),
                                                                                   (
                                                                                       '33333333-3333-3333-3333-333333333333',
                                                                                       '2026-03-11 10:00:00',
                                                                                       '2026-03-11 12:00:00',
                                                                                       2,
                                                                                       '22fae9b7-4311-4801-9906-ce20fc660c7b',
                                                                                       'ACTIVE'
                                                                                   )
    ON CONFLICT DO NOTHING;

-- Seed: booking_cleaner relations
INSERT INTO booking_cleaner (id, booking_id, cleaner_id) VALUES
                                                             (
                                                                 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
                                                                 '11111111-1111-1111-1111-111111111111',
                                                                 '2949580d-d1bf-4a22-966f-aaf79898fd3d'
                                                             ),
                                                             (
                                                                 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
                                                                 '11111111-1111-1111-1111-111111111111',
                                                                 'fa306ca1-4453-4ade-8084-762f7bf09e2f'
                                                             ),
                                                             (
                                                                 'bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbb1',
                                                                 '22222222-2222-2222-2222-222222222222',
                                                                 '3612b240-aee0-426d-8d47-cf0dff74fa92'
                                                             ),
                                                             (
                                                                 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbb2',
                                                                 '22222222-2222-2222-2222-222222222222',
                                                                 'b2ed2874-0e86-4e31-a197-26b35fa0396b'
                                                             ),
                                                             (
                                                                 'ccccccc1-cccc-cccc-cccc-ccccccccccc1',
                                                                 '33333333-3333-3333-3333-333333333333',
                                                                 '83e68582-78a4-4d0e-84eb-2e013c38825c'
                                                             ),
                                                             (
                                                                 'ccccccc2-cccc-cccc-cccc-ccccccccccc2',
                                                                 '33333333-3333-3333-3333-333333333333',
                                                                 'f4204e32-70fc-4c2c-aa8f-c97a1a8735a2'
                                                             )
    ON CONFLICT DO NOTHING;