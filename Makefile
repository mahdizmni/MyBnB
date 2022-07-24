all :
	@mysql -uroot -p MyBnB < init.sql && echo tables added.
	@mysql -uroot -p MyBnB < sample_user.sql  && echo sample user data loaded.
