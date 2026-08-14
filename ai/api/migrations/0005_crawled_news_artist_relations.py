# Generated manually: Flyway remains the physical-schema owner for shared tables.
from django.db import migrations, models
from django.db.migrations.exceptions import IrreversibleError
import django.db.models.deletion
import uuid


def create_relation_table(apps, schema_editor):
    vendor = schema_editor.connection.vendor
    if vendor == 'postgresql':
        schema_editor.execute('''
            CREATE TABLE IF NOT EXISTS crawled_news_artists (
                id UUID PRIMARY KEY,
                news_id UUID NOT NULL REFERENCES crawled_news(id) ON DELETE CASCADE,
                artist_id UUID NOT NULL,
                CONSTRAINT ux_crawled_news_artists_news_artist UNIQUE (news_id, artist_id)
            )
        ''')
        schema_editor.execute('''
            CREATE INDEX IF NOT EXISTS idx_crawled_news_artists_artist_id
            ON crawled_news_artists(artist_id)
        ''')
    elif vendor == 'sqlite':
        schema_editor.execute('''
            CREATE TABLE IF NOT EXISTS crawled_news_artists (
                id char(32) PRIMARY KEY,
                news_id char(32) NOT NULL REFERENCES crawled_news(id) ON DELETE CASCADE,
                artist_id char(32) NOT NULL,
                CONSTRAINT ux_crawled_news_artists_news_artist UNIQUE (news_id, artist_id)
            )
        ''')
        schema_editor.execute('''
            CREATE INDEX IF NOT EXISTS idx_crawled_news_artists_artist_id
            ON crawled_news_artists(artist_id)
        ''')


def reject_relation_table_rollback(apps, schema_editor):
    del apps, schema_editor
    raise IrreversibleError(
        'crawled_news_artists is owned by Spring Flyway and cannot be rolled back by Django'
    )


class Migration(migrations.Migration):
    dependencies = [
        ('api', '0004_model_slimming_phase2'),
    ]

    operations = [
        migrations.SeparateDatabaseAndState(
            database_operations=[],
            state_operations=[
                migrations.AlterField(
                    model_name='crawlednews',
                    name='url',
                    field=models.CharField(max_length=500, unique=True),
                ),
            ],
        ),
        migrations.SeparateDatabaseAndState(
            database_operations=[migrations.RunPython(
                create_relation_table,
                reject_relation_table_rollback,
            )],
            state_operations=[
                migrations.CreateModel(
                    name='CrawledNewsArtist',
                    fields=[
                        ('id', models.UUIDField(default=uuid.uuid4, editable=False, primary_key=True, serialize=False)),
                        ('artist_id', models.UUIDField()),
                        ('news', models.ForeignKey(
                            db_column='news_id',
                            on_delete=django.db.models.deletion.CASCADE,
                            related_name='artist_relations',
                            to='api.crawlednews',
                        )),
                    ],
                    options={
                        'db_table': 'crawled_news_artists',
                        'constraints': [
                            models.UniqueConstraint(
                                fields=('news', 'artist_id'),
                                name='ux_crawled_news_artists_news_artist',
                            )
                        ],
                    },
                ),
            ],
        ),
    ]
