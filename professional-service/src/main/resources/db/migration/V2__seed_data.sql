-- Seed: 5 vehicles
INSERT INTO vehicle (id, code, license_plate) VALUES
                                                  ('5b2b9322-3a9a-42b0-84a0-7d27af53dea4', 'VEH-01', 'JLF-1001'),
                                                  ('6c84d63c-cfb9-4088-933f-66c837794787', 'VEH-02', 'JLF-1002'),
                                                  ('22fae9b7-4311-4801-9906-ce20fc660c7b', 'VEH-03', 'JLF-1003'),
                                                  ('81ca6396-fa90-4267-a555-75080e08857f', 'VEH-04', 'JLF-1004'),
                                                  ('9cd3df86-8c5c-42ce-b311-38bfcf249963', 'VEH-05', 'JLF-1005')
    ON CONFLICT DO NOTHING;

-- Seed: 25 cleaners (5 per vehicle)
INSERT INTO cleaner (id, full_name, vehicle_id) VALUES
                                                    ('2949580d-d1bf-4a22-966f-aaf79898fd3d', 'Cleaner 01-01', '5b2b9322-3a9a-42b0-84a0-7d27af53dea4'),
                                                    ('fa306ca1-4453-4ade-8084-762f7bf09e2f', 'Cleaner 01-02', '5b2b9322-3a9a-42b0-84a0-7d27af53dea4'),
                                                    ('f5a8bd02-e328-4f72-a539-011cca949f3b', 'Cleaner 01-03', '5b2b9322-3a9a-42b0-84a0-7d27af53dea4'),
                                                    ('cb8968a5-c06d-409b-80a8-08a1c72a48ed', 'Cleaner 01-04', '5b2b9322-3a9a-42b0-84a0-7d27af53dea4'),
                                                    ('0bdcb6e9-da44-4cfc-bb68-aa15e6cd608f', 'Cleaner 01-05', '5b2b9322-3a9a-42b0-84a0-7d27af53dea4'),

                                                    ('3612b240-aee0-426d-8d47-cf0dff74fa92', 'Cleaner 02-01', '6c84d63c-cfb9-4088-933f-66c837794787'),
                                                    ('b2ed2874-0e86-4e31-a197-26b35fa0396b', 'Cleaner 02-02', '6c84d63c-cfb9-4088-933f-66c837794787'),
                                                    ('3638acf9-8297-4237-915b-ab1bff4ae7ce', 'Cleaner 02-03', '6c84d63c-cfb9-4088-933f-66c837794787'),
                                                    ('78a95903-a1d4-40a0-bc69-49bd7d9b954d', 'Cleaner 02-04', '6c84d63c-cfb9-4088-933f-66c837794787'),
                                                    ('a65b69e5-85ba-424d-8f39-3d0fc720578c', 'Cleaner 02-05', '6c84d63c-cfb9-4088-933f-66c837794787'),

                                                    ('83e68582-78a4-4d0e-84eb-2e013c38825c', 'Cleaner 03-01', '22fae9b7-4311-4801-9906-ce20fc660c7b'),
                                                    ('f4204e32-70fc-4c2c-aa8f-c97a1a8735a2', 'Cleaner 03-02', '22fae9b7-4311-4801-9906-ce20fc660c7b'),
                                                    ('8a884bd6-651e-408c-8c65-13089b7ac17b', 'Cleaner 03-03', '22fae9b7-4311-4801-9906-ce20fc660c7b'),
                                                    ('7d379e99-5226-4fe7-a204-c848270773ec', 'Cleaner 03-04', '22fae9b7-4311-4801-9906-ce20fc660c7b'),
                                                    ('4e4ce5d5-4aa7-44fb-a977-24bf4c8e8ccd', 'Cleaner 03-05', '22fae9b7-4311-4801-9906-ce20fc660c7b'),

                                                    ('7c90aef0-9c6b-4efd-ac93-7c96d1a893c1', 'Cleaner 04-01', '81ca6396-fa90-4267-a555-75080e08857f'),
                                                    ('f758171a-4db5-4107-af59-575f3856563c', 'Cleaner 04-02', '81ca6396-fa90-4267-a555-75080e08857f'),
                                                    ('1c3dc639-2157-458f-b08e-7bb184656925', 'Cleaner 04-03', '81ca6396-fa90-4267-a555-75080e08857f'),
                                                    ('6e43c8dd-dd3a-4a80-a414-940a593193b5', 'Cleaner 04-04', '81ca6396-fa90-4267-a555-75080e08857f'),
                                                    ('c209ea4c-9349-43bf-86ae-e9e29f04f094', 'Cleaner 04-05', '81ca6396-fa90-4267-a555-75080e08857f'),

                                                    ('7828d8e9-a41e-4f4c-83cd-06b90cbece4c', 'Cleaner 05-01', '9cd3df86-8c5c-42ce-b311-38bfcf249963'),
                                                    ('47cbbc2d-da3d-486b-9e47-a3a32dabb674', 'Cleaner 05-02', '9cd3df86-8c5c-42ce-b311-38bfcf249963'),
                                                    ('25f03770-ca7a-408c-81fb-93cf494bb5c4', 'Cleaner 05-03', '9cd3df86-8c5c-42ce-b311-38bfcf249963'),
                                                    ('27d86c53-c072-4916-b90f-7f39d6631835', 'Cleaner 05-04', '9cd3df86-8c5c-42ce-b311-38bfcf249963'),
                                                    ('3004fc94-def3-443b-aaa9-25806686cbce', 'Cleaner 05-05', '9cd3df86-8c5c-42ce-b311-38bfcf249963')
    ON CONFLICT DO NOTHING;