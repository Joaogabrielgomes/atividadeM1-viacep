ALTER TABLE product ADD COLUMN distribution_center VARCHAR(100);

UPDATE product SET distribution_center = 'Mogi das Cruzes' WHERE id IN ('p1', 'p2', 'p3', 'p4', 'p5', 'p6', 'p7');
UPDATE product SET distribution_center = 'Recife'          WHERE id IN ('p8', 'p9', 'p10', 'p11', 'p12', 'p13');
UPDATE product SET distribution_center = 'Porto Alegre'    WHERE id IN ('p14', 'p15', 'p16', 'p17', 'p18', 'p19', 'p20');
