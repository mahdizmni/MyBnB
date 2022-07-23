all :
	@mysql -uroot -p MyBnB < init.sql && echo tables added.
	@python bulkloader.py && echo sample user data created.
	@mysql -uroot -p MyBnB < sample.sql  && echo sample user data loaded.
